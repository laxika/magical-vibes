package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.c.CreepingMold;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfTheVault.class, AetherSpellbomb.class, CreepingMold.class, RuleOfLaw.class})
class DiscipleOfTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger makes a target opponent lose 1 life")
    void acceptingTriggerMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.setLife(player2, 20);

        destroyPermanentWithCreepingMold(player2, "Aether Spellbomb");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Declining the trigger does not make an opponent lose life")
    void decliningTriggerDoesNotMakeOpponentLoseLife() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.setLife(player2, 20);

        destroyPermanentWithCreepingMold(player2, "Aether Spellbomb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An artifact controlled by the ability controller also triggers the ability")
    void artifactControlledByAbilityControllerAlsoTriggersAbility() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.setLife(player2, 20);

        destroyPermanentWithCreepingMold(player1, "Aether Spellbomb");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Putting an enchantment into a graveyard does not trigger the ability")
    void enchantmentPutIntoGraveyardDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setLife(player2, 20);

        destroyPermanentWithCreepingMold(player2, "Rule of Law");

        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void destroyPermanentWithCreepingMold(Player targetPlayer, String targetName) {
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(targetPlayer, targetName));
    }
}
