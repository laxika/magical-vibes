package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MirrormindCrownTest extends BaseCardTest {

    @Test
    @DisplayName("The first token creation each turn may create copies of the equipped creature")
    void replacesFirstTokenCreationWithEquippedCreatureCopies() {
        setupCrownAndJadeMage();
        addJadeMageActivationMana();

        activateJadeMage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Declining the replacement creates the original tokens and uses the Crown for the turn")
    void declineCreatesOriginalTokensAndDoesNotOfferAgain() {
        setupCrownAndJadeMage();
        addJadeMageActivationMana();

        activateJadeMage();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        activateJadeMage();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Saproling"))
                .hasSize(2);
    }

    private void setupCrownAndJadeMage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new JadeMage());
    }

    private void addJadeMageActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateJadeMage() {
        harness.activateAbility(player1, 2, 0, null);
        harness.passBothPriorities();
    }
}
