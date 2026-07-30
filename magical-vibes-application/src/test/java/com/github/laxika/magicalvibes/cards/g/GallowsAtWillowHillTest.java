package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GallowsAtWillowHillTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted creature and gives its controller a 1/1 white flying Spirit")
    void destroysTargetAndGivesSpirit() {
        Permanent gallows = addPermanent(player1, new GallowsAtWillowHill());
        addHumans(player1, 4);
        Permanent bears = addPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        tapHumans(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gallows.isTapped()).isTrue();
        assertThat(tappedHumanCount(player1)).isEqualTo(3);

        List<Permanent> spirits = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    @DisplayName("A non-creature permanent cannot be targeted")
    void cannotTargetNonCreature() {
        addPermanent(player1, new GallowsAtWillowHill());
        addHumans(player1, 4);
        Permanent artifact = addPermanent(player2, createCard("Test Artifact", CardType.ARTIFACT));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three untapped Humans")
    void cannotActivateWithoutThreeHumans() {
        addPermanent(player1, new GallowsAtWillowHill());
        addHumans(player1, 2);
        Permanent bears = addPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Human creatures cannot pay the tap cost")
    void nonHumansCannotPayCost() {
        addPermanent(player1, new GallowsAtWillowHill());
        addHumans(player1, 2);
        addPermanent(player1, new GrizzlyBears());
        Permanent bears = addPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
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
