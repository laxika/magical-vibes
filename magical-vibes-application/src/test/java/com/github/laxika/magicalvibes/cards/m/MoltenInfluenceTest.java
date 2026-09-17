package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.c.CeaseFire;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenInfluence.class, CeaseFire.class, Firebolt.class, AvenFisher.class})
class MoltenInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant when its controller declines the damage")
    void countersWhenControllerDeclinesDamage() {
        CeaseFire ceaseFire = castCeaseFireAndMoltenInfluence();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Cease-Fire");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(ceaseFire.getId()));
    }

    @Test
    @DisplayName("Deals 4 damage and lets the instant resolve when its controller accepts")
    void dealsDamageWhenControllerAccepts() {
        castCeaseFireAndMoltenInfluence();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 16);
        harness.assertNotInGraveyard(player1, "Cease-Fire");

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Cease-Fire");
    }

    @Test
    @DisplayName("Counters a sorcery when its controller declines the damage")
    void countersSorceryWhenControllerDeclinesDamage() {
        Firebolt firebolt = castFireboltAndMoltenInfluence();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Firebolt");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(firebolt.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        AvenFisher avenFisher = new AvenFisher();
        harness.setHand(player1, List.of(avenFisher));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new MoltenInfluence()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, avenFisher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private CeaseFire castCeaseFireAndMoltenInfluence() {
        CeaseFire ceaseFire = new CeaseFire();
        harness.setHand(player1, List.of(ceaseFire));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new MoltenInfluence()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ceaseFire.getId());
        return ceaseFire;
    }

    private Firebolt castFireboltAndMoltenInfluence() {
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new MoltenInfluence()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firebolt.getId());
        return firebolt;
    }
}
