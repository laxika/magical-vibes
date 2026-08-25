package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArachnePsionicWeaver.class, GrizzlyBears.class, Shock.class})
class ArachnePsionicWeaverTest extends BaseCardTest {

    @Test
    void looksAtOpponentHandAndChoosesNoncreatureCardType() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new ArachnePsionicWeaver()));
        addArachneMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardType.CREATURE.name());

        harness.handleListChoice(player1, CardType.INSTANT.name());

        Permanent arachne = findPermanent(player1, "Arachne, Psionic Weaver");
        assertThat(arachne.getChosenCardType()).isEqualTo(CardType.INSTANT);
    }

    @Test
    void chosenTypeTaxesMatchingSpellsForAllPlayers() {
        addReadyArachne(player1, CardType.INSTANT);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void spellsOfOtherTypesAreNotTaxed() {
        addReadyArachne(player1, CardType.INSTANT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void addArachneMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyArachne(Player player, CardType chosenType) {
        Permanent arachne = new Permanent(new ArachnePsionicWeaver());
        arachne.setSummoningSick(false);
        arachne.setChosenCardType(chosenType);
        gd.playerBattlefields.get(player.getId()).add(arachne);
        return arachne;
    }
}
