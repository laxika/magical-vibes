package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KykarZephyrAwakener.class, GrizzlyBears.class, Spellbook.class})
class KykarZephyrAwakenerTest extends BaseCardTest {

    private static final String FLICKER =
            "Exile another target creature you control. Return that card at the beginning of the next end step";
    private static final String TOKEN = "Create a 1/1 white Spirit creature token with flying";

    @Test
    @DisplayName("Choosing the token mode creates a flying Spirit")
    void createsSpiritToken() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        castNoncreatureSpell();

        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing the flicker mode returns another creature at the next end step")
    void flickersAnotherCreatureUntilNextEndStep() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent kykar = findPermanent(player1, "Kykar, Zephyr Awakener");

        castNoncreatureSpell();
        harness.handleListChoice(player1, FLICKER);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId())
                .doesNotContain(kykar.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Kykar")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner even after Kykar leaves")
    void stolenCreatureReturnsToOwnerWithoutKykar() {
        Permanent kykar = harness.addToBattlefieldAndReturn(player1, new KykarZephyrAwakener());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        castNoncreatureSpell();
        harness.handleListChoice(player1, FLICKER);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kykar));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("Flickering a Spirit token exiles it without returning it")
    void flickeredTokenDoesNotReturn() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        castNoncreatureSpell();
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(spirit.getEffectivePower()).isEqualTo(1);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(1);
        harness.passBothPriorities();

        castNoncreatureSpell();
        harness.handleListChoice(player1, FLICKER);
        harness.handlePermanentChosen(player1, spirit.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spirit");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("A flicker target that changes controller becomes illegal")
    void targetMustStillBeControlledAtResolution() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();
        harness.handleListChoice(player1, FLICKER);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isEqualTo(target.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isEqualTo(target.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Kykar")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KykarZephyrAwakener());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castNoncreatureSpell() {
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }
}
