package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DinosaurHeaddress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaleontologistsPickAxe.class, DinosaurHeaddress.class, Forest.class,
        GrizzlyBears.class, HillGiant.class})
class PaleontologistsPickAxeTest extends BaseCardTest {

    @Test
    void equippedCreatureAttackingDrawsThenDiscards() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new PaleontologistsPickAxe());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card discarded = new HillGiant();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(axe.getAttachedTo()).isEqualTo(attacker.getId());
    }

    @Test
    void unequippedCreatureAttackingDoesNotLoot() {
        harness.addToBattlefield(player1, new PaleontologistsPickAxe());
        addCreatureReady(player1, new GrizzlyBears());
        Card handCard = new HillGiant();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void headdressCopiesMaterialAsItAttachesWithoutAnotherStackResolution() {
        harness.addToBattlefield(player1, new PaleontologistsPickAxe());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(material.getCard().getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftAcceptsMultipleMaterialsFromBattlefieldAndGraveyard() {
        harness.addToBattlefield(player1, new PaleontologistsPickAxe());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card graveyardMaterial = new HillGiant();
        harness.setGraveyard(player1, List.of(graveyardMaterial));
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1,
                List.of(material.getCard().getId(), graveyardMaterial.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardMaterial.getId()));

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardMaterial);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(material);
    }

    @Test
    void craftReturnsTransformedHeaddressAttachedToAChosenCreatureAndCopiesCraftMaterial() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new PaleontologistsPickAxe());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(material.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent headdress = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof DinosaurHeaddress)
                .findFirst()
                .orElseThrow();
        assertThat(headdress.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(axe, material);
    }
}
