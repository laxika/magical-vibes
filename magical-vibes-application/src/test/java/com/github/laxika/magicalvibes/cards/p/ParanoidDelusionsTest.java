package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParanoidDelusionsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills three cards and cipher can be declined")
    void targetMillsThreeAndCipherDeclined() {
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, List.of(new ParanoidDelusions()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Paranoid Delusions");
    }

    @Test
    @DisplayName("A library with fewer than three cards is emptied")
    void smallLibraryIsEmptied() {
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new ParanoidDelusions()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Encoded copy mills the chosen player again after combat damage")
    void cipherCopyOnCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.setLibrary(player2, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, List.of(new ParanoidDelusions()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Paranoid Delusions"));
        harness.assertNotInGraveyard(player1, "Paranoid Delusions");

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // CR 601.2c — the cipher copy is cast, so its controller chooses its target anew.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }
}
