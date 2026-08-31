package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.cards.p.PowerstoneShard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonlitMeditation.class, GrizzlyBears.class, JadeMage.class, PowerstoneShard.class})
class MoonlitMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("The first token creation each turn may create copies of the enchanted creature")
    void replacesFirstTokenCreationWithEnchantedCreatureCopies() {
        Permanent enchanted = setupMoonlitMeditation(new GrizzlyBears());
        addJadeMageActivationMana();

        activateJadeMage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals(enchanted.getCard().getName())
                        && permanent.getCard().getPower() == enchanted.getCard().getPower()
                        && permanent.getCard().getToughness() == enchanted.getCard().getToughness());
    }

    @Test
    @DisplayName("The replacement can copy an enchanted artifact")
    void replacesTokenCreationWithEnchantedArtifactCopies() {
        Permanent enchanted = setupMoonlitMeditation(new PowerstoneShard());
        addJadeMageActivationMana();

        activateJadeMage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals(enchanted.getCard().getName())
                        && permanent.getCard().hasType(enchanted.getCard().getType()));
    }

    @Test
    @DisplayName("Declining the replacement creates the original tokens and uses it for the turn")
    void declineCreatesOriginalTokensAndDoesNotOfferAgain() {
        setupMoonlitMeditation(new GrizzlyBears());
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

    @Test
    @DisplayName("Cannot enchant an artifact or creature controlled by an opponent")
    void cannotEnchantOpponentsPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoonlitMeditation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature you control");
    }

    private Permanent setupMoonlitMeditation(com.github.laxika.magicalvibes.model.Card enchantedCard) {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, enchantedCard);
        Permanent moonlit = harness.addToBattlefieldAndReturn(player1, new MoonlitMeditation());
        moonlit.setAttachedTo(enchanted.getId());
        harness.addToBattlefield(player1, new JadeMage());
        return enchanted;
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
