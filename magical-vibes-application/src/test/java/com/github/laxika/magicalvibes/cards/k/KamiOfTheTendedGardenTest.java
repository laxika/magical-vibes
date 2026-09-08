package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfTheTendedGardenTest extends BaseCardTest {

    private void wrathToKillKami() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining to pay {G} sacrifices Kami of the Tended Garden")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new KamiOfTheTendedGarden());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Kami of the Tended Garden");
        harness.assertInGraveyard(player1, "Kami of the Tended Garden");
    }

    @Test
    @DisplayName("Paying {G} keeps Kami of the Tended Garden on the battlefield")
    void payKeeps() {
        harness.addToBattlefield(player1, new KamiOfTheTendedGarden());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Kami of the Tended Garden");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less to hand")
    void soulshiftReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new KamiOfTheTendedGarden());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        wrathToKillKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(spirit);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift does not offer a Spirit with mana value 4 or greater")
    void soulshiftRejectsExpensiveSpirit() {
        harness.addToBattlefield(player1, new KamiOfTheTendedGarden());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new BattlegroundGeist())));

        wrathToKillKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
