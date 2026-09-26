package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rootrunner.class, Forest.class, LanternKami.class, KamiOfTheHunt.class})
class RootrunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices Rootrunner and puts target land on top of its owner's library")
    void abilityTucksTargetLand() {
        harness.addToBattlefield(player1, new Rootrunner());
        Card land = new Forest();
        harness.addToBattlefield(player2, land);
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, landId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rootrunner");
        harness.assertInGraveyard(player1, "Rootrunner");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(landId));
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Ability cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Rootrunner());
        harness.addToBattlefield(player2, new LanternKami());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Lantern Kami")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a land");
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less when Rootrunner dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new Rootrunner());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Forest"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new Rootrunner());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Forest"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift can be declined even when a legal Spirit target exists")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new Rootrunner());
        harness.addToBattlefield(player2, new Forest());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Forest"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift targets only your Spirits with mana value 3 or less")
    void soulshiftFiltersByTypeManaValueAndController() {
        harness.addToBattlefield(player1, new Rootrunner());
        harness.addToBattlefield(player2, new Forest());
        Card boundarySpirit = new KamiOfTheHunt();
        Card nonSpirit = new Forest();
        Card expensiveSpirit = new Rootrunner();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(boundarySpirit, nonSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Forest"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(boundarySpirit.getId());
    }
}
