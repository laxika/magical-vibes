package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SengirNosferatu.class})
class SengirNosferatuTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling Sengir Nosferatu creates a 1/2 flying Bat token")
    void exilingSelfCreatesBatToken() {
        activateNosferatu();

        harness.assertNotOnBattlefield(player1, "Sengir Nosferatu");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Sengir Nosferatu");

        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Bat returns an exiled Sengir Nosferatu under its owner's control")
    void sacrificingBatReturnsNosferatu() {
        activateNosferatu();
        Permanent bat = findPermanent(player1, "Bat");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bat), 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sengir Nosferatu");
        harness.assertNotOnBattlefield(player1, "Bat");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing among multiple exiled Sengir Nosferatus returns only the chosen card")
    void choosesOneOfMultipleExiledNosferatus() {
        addReadyNosferatu();
        addReadyNosferatu();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Bat");
        List<UUID> exiledIds = gd.exiledCards.stream()
                .filter(entry -> entry.card().getName().equals("Sengir Nosferatu"))
                .map(entry -> entry.card().getId())
                .toList();
        assertThat(exiledIds).hasSize(2);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bat), 0, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(exiledIds);

        harness.handleMultipleCardsChosen(player1, List.of(exiledIds.getFirst()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Sengir Nosferatu")))
                .hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Sengir Nosferatu");
    }

    private void addReadyNosferatu() {
        Permanent permanent = new Permanent(new SengirNosferatu());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
    }

    private void activateNosferatu() {
        addReadyNosferatu();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
