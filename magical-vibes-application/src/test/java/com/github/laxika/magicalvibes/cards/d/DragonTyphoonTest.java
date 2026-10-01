package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoltwingMarauder;
import com.github.laxika.magicalvibes.cards.c.CalderaPyremaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaticHellkite;
import com.github.laxika.magicalvibes.cards.n.NerivHeartOfTheStorm;
import com.github.laxika.magicalvibes.cards.s.StormscaleScion;
import com.github.laxika.magicalvibes.cards.t.ThunderbreakRegent;
import com.github.laxika.magicalvibes.cards.t.ThundermaneDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({DragonTyphoon.class, BoltwingMarauder.class, CalderaPyremaw.class,
        GrizzlyBears.class, MagmaticHellkite.class, NerivHeartOfTheStorm.class,
        Shock.class, StormscaleScion.class, ThunderbreakRegent.class,
        ThundermaneDragon.class})
class DragonTyphoonTest extends BaseCardTest {

    @Test
    void draftsADragonOntoTheBattlefieldForANoncreatureSpell() {
        harness.addToBattlefield(player1, new DragonTyphoon());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);

        Card drafted = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(drafted.getId()));
    }

    @Test
    void doesNotTriggerForNonDragonCreatureSpells() {
        harness.addToBattlefield(player1, new DragonTyphoon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void channelCreatesAFlyingDragonAndDiscardsDragonTyphoon() {
        harness.setHand(player1, List.of(new DragonTyphoon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Dragon Typhoon");
    }
}
