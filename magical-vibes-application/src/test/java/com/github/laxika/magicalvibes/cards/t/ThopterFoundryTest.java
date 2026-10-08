package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThopterFoundry.class, Spellbook.class})
class ThopterFoundryTest extends BaseCardTest {

    private long thopterTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .count();
    }

    @Test
    @DisplayName("Self-sacrifice pays the cost immediately and resolves without the Foundry")
    void selfSacrificeResolvesAndDoesNotUseOpponentsArtifact() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Thopter Foundry");
        harness.assertInGraveyard(player1, "Thopter Foundry");
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(thopterTokens()).isZero();
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        assertThat(thopterTokens()).isEqualTo(1);
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
        Permanent token = findPermanent(player1, "Thopter");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Activation requires one mana and cannot sacrifice an artifact for free")
    void activationWithoutManaIsRejected() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Thopter Foundry");
        harness.assertNotInGraveyard(player1, "Thopter Foundry");
        assertThat(thopterTokens()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped Foundry can activate repeatedly without a tap cost")
    void tappedFoundryCanActivateRepeatedly() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        findPermanent(player1, "Thopter Foundry").tap();
        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Thopter Foundry");
        assertThat(thopterTokens()).isEqualTo(2);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Sacrificing a nontoken artifact creates a Thopter token and gains 1 life")
    void sacrificeCreatesThopterAndGainsLife() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // Two nontoken artifacts (the Foundry itself + Spellbook) → choose which to sacrifice.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(thopterTokens()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        // The chosen artifact is sacrificed; the Foundry stays.
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Thopter Foundry");
    }

    @Test
    @DisplayName("Created token is a 1/1 blue Thopter artifact creature with flying")
    void thopterTokenCharacteristics() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A token artifact is not a legal sacrifice (only the nontoken Foundry is offered)")
    void tokenArtifactCannotBeSacrificed() {
        harness.addToBattlefield(player1, new ThopterFoundry());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // First activation sacrifices Spellbook, leaving the Foundry + a Thopter artifact token.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();
        assertThat(thopterTokens()).isEqualTo(1);

        // Second activation: the only legal nontoken artifact is the Foundry itself, so it is
        // auto-sacrificed with no choice prompt — the Thopter token is not an eligible sacrifice.
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice)
                .isFalse();
        harness.assertNotOnBattlefield(player1, "Thopter Foundry");
        assertThat(thopterTokens()).isEqualTo(1);
    }
}
