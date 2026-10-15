package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FontOfReturn.class, GrizzlyBears.class, LightningBolt.class})
class FontOfReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns up to three target creature cards")
    void sacrificesItselfAndReturnsThreeCreatureCards() {
        Permanent font = addFont();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third)));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(font), 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
    }

    @Test
    @DisplayName("Allows returning fewer than three creature cards")
    void returnsFewerThanThreeCreatureCards() {
        Permanent font = addFont();
        Card bears = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, other)));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(font), 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bears.getId()).doesNotContain(other.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    @DisplayName("Rejects noncreature and opponent graveyard targets")
    void rejectsIllegalTargets() {
        Permanent font = addFont();
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bolt)));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(font), 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);

        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(font), 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsZeroTargetsAndStillPaysCosts() {
        Permanent font = addFont();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(font), 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(handIds(player1)).doesNotContain(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsMoreThanThreeTargets() {
        Permanent font = addFont();
        List<Card> creatures = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, creatures);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(font), 0, creatures.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
    }

    @Test
    void rejectsDuplicateTargets() {
        Permanent font = addFont();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(font), 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
    }

    @Test
    void returnsRemainingLegalTargetsWhenOneLeavesGraveyard() {
        Permanent font = addFont();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        addActivationMana();
        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(font), 0,
                List.of(first.getId(), second.getId()));

        gd.playerGraveyards.get(player1.getId()).remove(first);
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(second.getId()).doesNotContain(first.getId());
        assertThat(gd.findExiledCard(first.getId()).card()).isSameAs(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard()).doesNotContain(second);
    }

    @Test
    void doesNotReturnTargetsThatHaveAllLeftGraveyard() {
        Permanent font = addFont();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();
        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(font), 0,
                List.of(creature.getId()));

        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(handIds(player1)).doesNotContain(creature.getId());
        assertThat(gd.findExiledCard(creature.getId()).card()).isSameAs(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFont() {
        return harness.addToBattlefieldAndReturn(player1, new FontOfReturn());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int battlefieldIndex(Permanent font) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(font);
    }

    private List<java.util.UUID> handIds(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
