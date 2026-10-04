package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecroticHex.class, GrizzlyBears.class})
class NecroticHexTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices up to six creatures and the caster creates six tapped Zombies")
    void eachPlayerSacrificesUpToSixCreaturesAndCreatesTappedZombies() {
        addCreatures(player1, 6);
        addCreatures(player2, 4);

        castNecroticHex();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        assertThat(findPermanents(player1, "Zombie"))
                .hasSize(6)
                .allSatisfy(zombie -> assertThat(zombie.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Each player chooses six creatures when they control more than six")
    void eachPlayerChoosesSixCreatures() {
        addCreatures(player1, 7);
        addCreatures(player2, 7);

        castNecroticHex();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.maxCount()).isEqualTo(6);
        assertThat(firstChoice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        List<Permanent> player1Creatures = findPermanents(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1,
                player1Creatures.subList(0, 6).stream().map(Permanent::getId).toList());

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.maxCount()).isEqualTo(6);

        List<Permanent> player2Creatures = findPermanents(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2,
                player2Creatures.subList(0, 6).stream().map(Permanent::getId).toList());

        assertThat(countPermanents(player1, "Grizzly Bears")).isOne();
        assertThat(countPermanents(player2, "Grizzly Bears")).isOne();
        assertThat(findPermanents(player1, "Zombie")).hasSize(6);
    }

    private void castNecroticHex() {
        harness.setHand(player1, List.of(new NecroticHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GrizzlyBears());
        }
    }
}
