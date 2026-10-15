package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DissectionTools.class, BalemurkLeech.class, Forest.class})
class DissectionToolsTest extends BaseCardTest {

    @Test
    void manifestsAndAttachesToTheManifestedCreature() {
        Card manifestedCard = new BalemurkLeech();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new DissectionTools()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent equipment = findPermanent(player1, "Dissection Tools");
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(equipment.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void equipSacrificesACreatureAndAttachesToTheTarget() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DissectionTools());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());

        harness.activateAbility(player1, 0, null, target.getId());

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(sacrificed.getId(), target.getId());
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void manifestsTheOnlyLibraryCardEvenWhenItIsNotACreatureCard() {
        Card manifestedCard = new Forest();
        harness.setHand(player1, List.of(new DissectionTools()));
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(findPermanent(player1, "Dissection Tools").getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(manifestedCard);
    }

    @Test
    void emptyLibraryLeavesEquipmentUnattached() {
        harness.setHand(player1, List.of(new DissectionTools()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dissection Tools").getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sacrificingTheEquipTargetPaysTheCostButDoesNotAttachEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DissectionTools());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        harness.addToBattlefield(player1, new BalemurkLeech());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }
}
