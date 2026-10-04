package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.s.SecureTheWastes;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Electrosiphon.class, CounselOfTheSoratami.class, SecureTheWastes.class})
class ElectrosiphonTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell and grants energy equal to its mana value")
    void countersSpellAndGrantsEnergyEqualToManaValue() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Electrosiphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Electrosiphon");
    }

    @Test
    @DisplayName("Counters the spell before granting energy")
    void countersBeforeGrantingEnergy() {
        SecureTheWastes spell = new SecureTheWastes();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Electrosiphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        List<String> events = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(text -> text.equals("Secure the Wastes is countered.")
                        || text.contains("gets 1 energy counter(s)."))
                .toList();
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isEqualTo("Secure the Wastes is countered.");
        assertThat(events.get(1)).contains("gets 1 energy counter(s).");
    }

    @Test
    @DisplayName("Counts the chosen X in the target spell's mana value")
    void includesXInEnergyAmount() {
        SecureTheWastes spell = new SecureTheWastes();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new Electrosiphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 4, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        harness.assertInGraveyard(player1, "Secure the Wastes");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents both countering and energy gain")
    void grantsNoEnergyWhenTargetHasLeftStack() {
        SecureTheWastes spell = new SecureTheWastes();
        harness.setHand(player1, List.of(spell, new Electrosiphon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Electrosiphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Secure the Wastes");
        harness.assertInGraveyard(player2, "Electrosiphon");
        assertThat(gd.stack).isEmpty();
    }
}
