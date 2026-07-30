package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevoutChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles the targeted artifact and taps the Chaplain plus two Humans")
    void exilesTargetArtifact() {
        Permanent chaplain = addChaplainReady(player1);
        addHumans(player1, 2);
        Permanent artifact = addPermanent(player2, createCard("Test Artifact", CardType.ARTIFACT));

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        tapHumans(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(chaplain.isTapped()).isTrue();
        assertThat(tappedHumanCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability exiles the targeted enchantment")
    void exilesTargetEnchantment() {
        addChaplainReady(player1);
        addHumans(player1, 2);
        Permanent enchantment = addPermanent(player2, createCard("Test Enchantment", CardType.ENCHANTMENT));

        harness.activateAbility(player1, 0, 0, null, enchantment.getId());
        tapHumans(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("A creature cannot be targeted")
    void cannotTargetCreature() {
        addChaplainReady(player1);
        addHumans(player1, 2);
        Permanent bears = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two other untapped Humans")
    void cannotActivateWithoutTwoHumans() {
        addChaplainReady(player1);
        addHumans(player1, 1);
        Permanent artifact = addPermanent(player2, createCard("Test Artifact", CardType.ARTIFACT));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Human creatures cannot pay the tap cost")
    void nonHumansCannotPayCost() {
        addChaplainReady(player1);
        addHumans(player1, 1);
        addPermanent(player1, new GrizzlyBears());
        Permanent artifact = addPermanent(player2, createCard("Test Artifact", CardType.ARTIFACT));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChaplainReady(Player player) {
        return addPermanent(player, new DevoutChaplain());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addHumans(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = createCard("Test Human " + i, CardType.CREATURE);
            card.setSubtypes(List.of(CardSubtype.HUMAN));
            card.setPower(1);
            card.setToughness(1);
            addPermanent(player, card);
        }
    }

    private Card createCard(String name, CardType type) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void tapHumans(Player player, int count) {
        List<Permanent> untapped = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent human : untapped) {
            harness.handlePermanentChosen(player, human.getId());
        }
    }

    private long tappedHumanCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().startsWith("Test Human"))
                .filter(Permanent::isTapped)
                .count();
    }
}
