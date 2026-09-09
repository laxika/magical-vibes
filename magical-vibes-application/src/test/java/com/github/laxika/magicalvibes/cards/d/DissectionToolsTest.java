package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DissectionTools.class, GrizzlyBears.class, Forest.class})
class DissectionToolsTest extends BaseCardTest {

    @Test
    void manifestsAndAttachesToTheManifestedCreature() {
        Card manifestedCard = new GrizzlyBears();
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
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
}
