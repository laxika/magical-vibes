package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanOffering.class})
class SylvanOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Each paragraph independently chooses an opponent and creates its tokens")
    void independentlyChoosesOpponents() {
        var player3 = addThirdPlayer();
        harness.setHand(player1, List.of(new SylvanOffering()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validPlayerIds()).containsExactly(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(findPermanents(player1, "Treefolk")).hasSize(1);
        assertThat(findPermanents(player2, "Treefolk")).hasSize(1);
        assertThat(findPermanents(player3, "Treefolk")).isEmpty();
        assertThat(findPermanents(player1, "Treefolk").getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(findPermanents(player2, "Treefolk").getFirst().getEffectiveToughness()).isEqualTo(2);

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.validPlayerIds()).containsExactly(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player3.getId());

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);
        assertThat(findPermanents(player2, "Elf Warrior")).isEmpty();
        assertThat(findPermanents(player3, "Elf Warrior")).hasSize(2);
        assertThat(findPermanents(player1, "Elf Warrior")).allSatisfy(this::assertElfWarrior);
        assertThat(findPermanents(player3, "Elf Warrior")).allSatisfy(this::assertElfWarrior);
    }

    @Test
    @DisplayName("X zero creates no tokens")
    void zeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new SylvanOffering()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treefolk")).isEmpty();
        assertThat(findPermanents(player2, "Treefolk")).isEmpty();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Elf Warrior")).isEmpty();
    }

    private void assertElfWarrior(Permanent token) {
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
