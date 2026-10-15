package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildRide.class, AngelicChorus.class, GrizzlyBears.class, Shock.class})
class WildRideTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusThreeAndHaste() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildRide()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void boostAndHasteWearOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildRide()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new WildRide()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player1, "Angelic Chorus")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void harmonizeCastsFromGraveyardAndExilesTheSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        WildRide spell = new WildRide();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void harmonizeReducesGenericCostByTappedCreaturePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WildRide spell = new WildRide();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, List.of(target.getId()), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCanTapItsOwnTargetBeforeItGetsTheBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        WildRide spell = new WildRide();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, List.of(creature.getId()), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCannotReplaceTheRequiredRedMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WildRide spell = new WildRide();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeCannotTapAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new WildRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not on your battlefield");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setGraveyard(player1, List.of(new WildRide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastGoesToGraveyardWhenItsTargetDiesInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WildRide spell = new WildRide();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeExilesTheSpellWhenItsTargetDiesInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WildRide spell = new WildRide();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.stack).isEmpty();
    }
}
