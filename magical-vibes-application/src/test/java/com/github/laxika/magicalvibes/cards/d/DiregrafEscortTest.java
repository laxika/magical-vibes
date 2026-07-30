package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiregrafEscortTest extends BaseCardTest {

    private static Card zombie() {
        Card card = new Card();
        card.setName("Walking Corpse");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.ZOMBIE));
        return card;
    }

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiregrafEscort()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findEscort() {
        return findPermanent(player1, "Diregraf Escort");
    }

    @Test
    @DisplayName("While paired, both creatures have protection from Zombies")
    void pairedBothHaveProtectionFromZombies() {
        Permanent bears = castAndPairWithBears();
        Permanent escort = findEscort();
        Permanent zombie = new Permanent(zombie());
        gd.playerBattlefields.get(player2.getId()).add(zombie);

        assertThat(escort.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, bears, zombie)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Diregraf Escort has no protection from Zombies")
    void unpairedHasNoProtection() {
        harness.addToBattlefield(player1, new DiregrafEscort());
        Permanent escort = findEscort();
        Permanent zombie = new Permanent(zombie());
        gd.playerBattlefields.get(player2.getId()).add(zombie);

        assertThat(escort.getPairedWithId()).isNull();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isFalse();
    }

    @Test
    @DisplayName("Protection does not extend to non-Zombie sources")
    void noProtectionFromNonZombies() {
        Permanent bears = castAndPairWithBears();
        Permanent escort = findEscort();
        Permanent vanilla = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, vanilla)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, bears, vanilla)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both creatures unprotected")
    void decliningLeavesUnprotected() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiregrafEscort()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent escort = findEscort();
        Permanent zombie = new Permanent(zombie());
        gd.playerBattlefields.get(player2.getId()).add(zombie);

        assertThat(escort.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, bears, zombie)).isFalse();
    }
}
