package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AcclaimedContender;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BlacklanceParagon;
import com.github.laxika.magicalvibes.cards.c.CavalierOfDawn;
import com.github.laxika.magicalvibes.cards.c.CavalierOfNight;
import com.github.laxika.magicalvibes.cards.d.DauntlessBodyguard;
import com.github.laxika.magicalvibes.cards.g.GuardianOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheEbonLegion;
import com.github.laxika.magicalvibes.cards.m.MidnightReaper;
import com.github.laxika.magicalvibes.cards.m.MurderousRider;
import com.github.laxika.magicalvibes.cards.m.SmittenSwordmaster;
import com.github.laxika.magicalvibes.cards.o.OrderOfMidnight;
import com.github.laxika.magicalvibes.cards.t.TheCircleOfLoyalty;
import com.github.laxika.magicalvibes.cards.v.ValiantKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaddicTalZealot.class, KnightOfTheEbonLegion.class, GrizzlyBears.class,
        MidnightReaper.class, GuardianOfFaith.class, CavalierOfDawn.class, CavalierOfNight.class,
        BenalishMarshal.class, MurderousRider.class, OrderOfMidnight.class, AcclaimedContender.class,
        DauntlessBodyguard.class, ValiantKnight.class, SmittenSwordmaster.class,
        BlacklanceParagon.class, HistoryOfBenalia.class, TheCircleOfLoyalty.class})
class RaddicTalZealotTest extends BaseCardTest {

    @Test
    void attackingWithAKnightDraftsFromRaddicsSpellbook() {
        addCreatureReady(player1, new RaddicTalZealot());
        Permanent knight = addCreatureReady(player1, new KnightOfTheEbonLegion());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(Set.of(
                "Midnight Reaper", "Guardian of Faith", "Knight of the Ebon Legion", "Cavalier of Dawn",
                "Cavalier of Night", "Benalish Marshal", "Murderous Rider", "Order of Midnight",
                "Acclaimed Contender", "Dauntless Bodyguard", "Valiant Knight", "Smitten Swordmaster",
                "Blacklance Paragon", "History of Benalia", "The Circle of Loyalty")::contains);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    void attackingWithoutAKnightDoesNotDraft() {
        addCreatureReady(player1, new RaddicTalZealot());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
