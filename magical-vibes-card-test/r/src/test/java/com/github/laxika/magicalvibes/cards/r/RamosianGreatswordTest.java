package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarTrainedSlasher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosianGreatsword.class, GrizzlyBears.class, WarTrainedSlasher.class})
class RamosianGreatswordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(new GrizzlyBears());
        Permanent greatsword = addGreatswordReady();
        greatsword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {2} attaches Ramosian Greatsword to a creature you control")
    void equipAttachesToCreature() {
        Permanent greatsword = addGreatswordReady();
        Permanent creature = addCreatureReady(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(greatsword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Convoke lets Ramosian Greatsword be cast by tapping creatures")
    void castsWithConvoke() {
        List<Permanent> convokeCreatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new RamosianGreatsword()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                convokeCreatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof RamosianGreatsword);
        assertThat(convokeCreatures).allMatch(Permanent::isTapped);
    }

    @Test
    void reequippingMovesBoostAndTrample() {
        Permanent greatsword = addGreatswordReady();
        Permanent original = addCreatureReady(new GrizzlyBears());
        Permanent replacement = addCreatureReady(new GrizzlyBears());
        greatsword.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(greatsword.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotEquipOpponentCreature() {
        Permanent greatsword = addGreatswordReady();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(greatsword.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent greatsword = addGreatswordReady();
        Permanent creature = addCreatureReady(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(greatsword.getAttachedTo()).isNull();
    }

    @Test
    void redCreaturesConvokeEntireCostWhileSummoningSick() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new WarTrainedSlasher()),
                harness.addToBattlefieldAndReturn(player1, new WarTrainedSlasher()),
                harness.addToBattlefieldAndReturn(player1, new WarTrainedSlasher()),
                harness.addToBattlefieldAndReturn(player1, new WarTrainedSlasher()),
                harness.addToBattlefieldAndReturn(player1, new WarTrainedSlasher()));
        harness.setHand(player1, List.of(new RamosianGreatsword()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof RamosianGreatsword);
        assertThat(creatures).allMatch(Permanent::isTapped);
    }
    private Permanent addGreatswordReady() {
        return addCreatureReady(player1, new RamosianGreatsword());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player1, card);
    }
}
