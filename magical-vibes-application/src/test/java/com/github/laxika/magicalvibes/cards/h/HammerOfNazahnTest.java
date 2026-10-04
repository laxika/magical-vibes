package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerOfNazahn.class, GrizzlyBears.class, LeoninScimitar.class})
class HammerOfNazahnTest extends BaseCardTest {

    @Test
    @DisplayName("Hammer of Nazahn may attach itself when it enters")
    void attachesItselfWhenEntering() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HammerOfNazahn()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Hammer of Nazahn").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Hammer of Nazahn may attach another entering Equipment")
    void attachesAnotherEnteringEquipment() {
        Permanent hammer = addReady(player1, new HammerOfNazahn());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining Hammer of Nazahn's attachment leaves the Equipment unattached")
    void mayDeclineAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HammerOfNazahn()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Hammer of Nazahn").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0 and indestructible")
    void equippedCreatureGetsBonus() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent hammer = addReady(player1, new HammerOfNazahn());
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {4} attaches Hammer of Nazahn to a creature you control")
    void equipAttachesHammer() {
        Permanent hammer = addReady(player1, new HammerOfNazahn());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining attachment of another Equipment leaves it unattached")
    void mayDeclineOtherEquipmentAttachment() {
        addReady(player1, new HammerOfNazahn());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's entering Equipment does not trigger Hammer")
    void opponentEquipmentDoesNotTrigger() {
        addReady(player1, new HammerOfNazahn());
        addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LeoninScimitar()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Leonin Scimitar").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Hammer enters unattached when there is no legal creature target")
    void entersWithoutLegalTarget() {
        addReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HammerOfNazahn()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hammer of Nazahn").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Reequipping moves both the power bonus and indestructible")
    void reequippingMovesBonuses() {
        Permanent hammer = addReady(player1, new HammerOfNazahn());
        Permanent first = addReady(player1, new GrizzlyBears());
        Permanent second = addReady(player1, new GrizzlyBears());
        hammer.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
