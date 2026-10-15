package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ShrapnelSlinger;
import com.github.laxika.magicalvibes.cards.v.VanishIntoEternity;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldwardensHelm.class, ShrapnelSlinger.class, VanishIntoEternity.class})
class GoldwardensHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Goldwarden's Helm creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent helm = findPermanent(player1, "Goldwarden's Helm");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(rebel.getCard().isToken()).isTrue();
        assertThat(rebel.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(helm.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1}{W} moves Goldwarden's Helm and its bonus to another creature")
    void equipMovesHelmAndBonus() {
        harness.setHand(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new ShrapnelSlinger());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, slinger.getId());
        harness.passBothPriorities();

        Permanent helm = findPermanent(player1, "Goldwarden's Helm");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(helm.getAttachedTo()).isEqualTo(slinger.getId());
        assertThat(gqs.getEffectivePower(gd, slinger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, slinger)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    void createsRebelEvenIfHelmLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent helm = findPermanent(player1, "Goldwarden's Helm");
        harness.setHand(player2, List.of(new VanishIntoEternity()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0, helm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(helm.getOriginalCard().getId())).isNotNull();
        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    void failedEquipLeavesHelmOnOriginalCreature() {
        harness.setHand(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent helm = findPermanent(player1, "Goldwarden's Helm");
        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new ShrapnelSlinger());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, slinger.getId());

        harness.setHand(player2, List.of(new VanishIntoEternity()));
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.castInstant(player2, 0, slinger.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(slinger.getOriginalCard().getId())).isNotNull();
        assertThat(helm.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new GoldwardensHelm());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ShrapnelSlinger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(findPermanent(player1, "Goldwarden's Helm").getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresWhiteMana() {
        harness.addToBattlefield(player1, new GoldwardensHelm());
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new ShrapnelSlinger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, slinger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Goldwarden's Helm").getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipWhileEnterTriggerIsOnStack() {
        harness.setHand(player1, List.of(new GoldwardensHelm()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent slinger = harness.addToBattlefieldAndReturn(player1, new ShrapnelSlinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, slinger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Goldwarden's Helm").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Rebel").getId());
    }
}
