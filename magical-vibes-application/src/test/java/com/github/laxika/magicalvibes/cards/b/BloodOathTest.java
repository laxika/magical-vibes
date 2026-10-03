package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodOath.class, FreshVolunteers.class, JhovallRider.class, Counterspell.class, Forest.class,
        HengeGuardian.class})
class BloodOathTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing creature deals three damage for each creature card in the revealed hand")
    void dealsDamageForEachCardOfChosenType() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of(new FreshVolunteers(), new JhovallRider(), new Counterspell(), new Forest()));
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder(
                CardType.LAND.name(), CardType.CREATURE.name(), CardType.ENCHANTMENT.name(),
                CardType.SORCERY.name(), CardType.INSTANT.name(), CardType.ARTIFACT.name(),
                CardType.PLANESWALKER.name(), CardType.BATTLE.name(), CardType.KINDRED.name(),
                CardType.PLANE.name(), CardType.PHENOMENON.name(), CardType.SCHEME.name(),
                "CONSPIRACY", "DUNGEON", "VANGUARD");

        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.assertLife(player2, 14);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Choosing a type absent from the revealed hand deals no damage")
    void absentTypeDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of(new FreshVolunteers(), new Counterspell(), new Forest()));
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardType.PLANESWALKER.name());

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counts an artifact creature only once for a matching card type")
    void countsMultiTypeCardOnce() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of(new HengeGuardian()));
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardType.ARTIFACT.name());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Blood Oath can target only an opponent")
    void rejectsNonOpponentTarget() {
        harness.setHand(player1, List.of(new BloodOath()));
        addBloodOathMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller chooses a type before the opponent reveals their hand")
    void choosesTypeBeforeRevealingHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of(new FreshVolunteers()));
        addBloodOathMana();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("reveals their hand"));
        harness.assertLife(player2, 20);

        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Blood Oath");
    }

    @Test
    @DisplayName("An empty hand is revealed and causes no damage")
    void emptyHandDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of());
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.assertLife(player2, 20);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand. It is empty."));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Blood Oath");
    }

    @Test
    @DisplayName("Choosing land counts only lands in the opponent's hand")
    void countsLandCardsInTargetHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new FreshVolunteers(), new Counterspell()));
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardType.LAND.name());

        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Choosing instant counts instant cards rather than creature cards")
    void countsInstantCardsInTargetHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodOath()));
        harness.setHand(player2, List.of(new Counterspell(), new FreshVolunteers(), new HengeGuardian()));
        addBloodOathMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardType.INSTANT.name());

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    private void addBloodOathMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
