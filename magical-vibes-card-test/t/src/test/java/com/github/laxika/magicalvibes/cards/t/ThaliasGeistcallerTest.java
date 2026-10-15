package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.y.YawgmothsAgenda;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThaliasGeistcaller.class, YawgmothsAgenda.class, LightningStrike.class,
        LanternKami.class, GrizzlyBears.class})
class ThaliasGeistcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from your graveyard creates a 1/1 white flying Spirit")
    void createsSpiritWhenCastingFromGraveyard() {
        harness.addToBattlefield(player1, new ThaliasGeistcaller());
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent spirit = findSpiritToken();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, spirit)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Spirit grants indestructible until end of turn")
    void sacrificesSpiritForIndestructible() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliasGeistcaller());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new LanternKami());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thalia, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        harness.assertInGraveyard(player1, "Lantern Kami");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thalia, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot be paid with a non-Spirit")
    void cannotSacrificeNonSpirit() {
        harness.addToBattlefield(player1, new ThaliasGeistcaller());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from hand does not create a Spirit")
    void doesNotTriggerForHandCast() {
        harness.addToBattlefield(player1, new ThaliasGeistcaller());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent casting from their graveyard does not create a Spirit")
    void doesNotTriggerForOpponentGraveyardCast() {
        harness.addToBattlefield(player1, new ThaliasGeistcaller());
        harness.addToBattlefield(player2, new YawgmothsAgenda());
        harness.setGraveyard(player2, List.of(new LightningStrike()));
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The Spirit is created before the graveyard spell resolves and can pay the ability's cost")
    void canSacrificeNewTokenInResponseToGraveyardSpell() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliasGeistcaller());
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player1, 0, thalia.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(thalia.getMarkedDamage()).isZero();
        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gqs.hasKeyword(gd, thalia, Keyword.INDESTRUCTIBLE)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, thalia, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thalia);
        assertThat(thalia.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Spirit cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSpirit() {
        harness.addToBattlefield(player1, new ThaliasGeistcaller());
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spirit);
    }

    @Test
    @DisplayName("A tapped Spirit can be sacrificed while the Geistcaller is summoning sick")
    void canSacrificeTappedSpiritImmediately() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliasGeistcaller());
        thalia.setSummoningSick(true);
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        spirit.tap();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        assertThat(gqs.hasKeyword(gd, thalia, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The cast trigger still creates a Spirit if the Geistcaller dies in response")
    void triggerSurvivesSourceRemoval() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliasGeistcaller());
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.castInstant(player2, 0, thalia.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thalia's Geistcaller");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        harness.assertLife(player2, 17);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent findSpiritToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> gqs.effectiveCreatureSubtypes(gd, permanent).contains(CardSubtype.SPIRIT))
                .findFirst()
                .orElseThrow();
    }
}
