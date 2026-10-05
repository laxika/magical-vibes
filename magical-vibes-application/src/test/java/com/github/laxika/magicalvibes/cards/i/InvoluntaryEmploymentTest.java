package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.m.MesmericOrb;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvoluntaryEmployment.class, BearCub.class, Pacifism.class, MesmericOrb.class})
class InvoluntaryEmploymentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Involuntary Employment steals, untaps, and grants haste to a creature")
    void resolvesStealUntapHasteAndTreasure() {
        Permanent target = addCreatureReady(player2, new BearCub());
        target.tap();
        castInvoluntaryEmployment(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The stolen creature and granted haste return to normal at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new BearCub());
        castInvoluntaryEmployment(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The created token is a Treasure artifact")
    void createsTreasureArtifactToken() {
        Permanent target = addCreatureReady(player2, new BearCub());
        castInvoluntaryEmployment(target);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new InvoluntaryEmployment()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles when the target creature leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent target = addCreatureReady(player2, new BearCub());
        harness.setHand(player1, List.of(new InvoluntaryEmployment()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        target.tap();

        castInvoluntaryEmployment(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The stolen creature can attack immediately")
    void stolenCreatureCanAttackImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        castInvoluntaryEmployment(target);

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control changes before untapping, so Mesmeric Orb mills the new controller")
    void gainsControlBeforeUntapping() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent target = addCreatureReady(player2, new BearCub());
        target.tap();
        harness.setLibrary(player1, List.of(new BearCub()));
        harness.setLibrary(player2, List.of(new BearCub()));

        castInvoluntaryEmployment(target);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bear Cub");
        harness.assertNotInGraveyard(player2, "Bear Cub");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The Treasure can be sacrificed immediately for one mana of any color")
    void treasureProducesChosenColor(ManaColor color) {
        Permanent target = addCreatureReady(player2, new BearCub());
        castInvoluntaryEmployment(target);
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(color);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    private void castInvoluntaryEmployment(Permanent target) {
        harness.setHand(player1, List.of(new InvoluntaryEmployment()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
