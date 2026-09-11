package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TomBertAndWilliam.class, Disenchant.class, Forest.class, GoForTheThroat.class,
        GrizzlyBears.class, Shock.class, Swamp.class})
class TomBertAndWilliamTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws cards equal to its effective power, then discards")
    void sacrificeAbilityDrawsAndDiscards() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        sacrificed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Swamp(), new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tom);
    }

    @Test
    @DisplayName("When it dies as a creature, it returns as a noncreature artifact")
    void returnsAsNoncreatureArtifact() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        destroyCreature(tom);

        Permanent returned = findPermanent(player1, "Tom, Bert, and William");
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        harness.assertNotInGraveyard(player1, "Tom, Bert, and William");
    }

    @Test
    @DisplayName("A returned artifact does not trigger the creature death ability again")
    void returnedArtifactDoesNotReturnAgain() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        destroyCreature(tom);
        Permanent returned = findPermanent(player1, "Tom, Bert, and William");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tom, Bert, and William");
        harness.assertNotOnBattlefield(player1, "Tom, Bert, and William");
    }

    private void destroyCreature(Permanent permanent) {
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
