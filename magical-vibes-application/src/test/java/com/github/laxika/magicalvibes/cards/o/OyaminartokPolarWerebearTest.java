package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Archipelagore;
import com.github.laxika.magicalvibes.cards.j.JunkWinder;
import com.github.laxika.magicalvibes.cards.m.MoatPiranhas;
import com.github.laxika.magicalvibes.cards.m.MysticSkyfish;
import com.github.laxika.magicalvibes.cards.n.NadirKraken;
import com.github.laxika.magicalvibes.cards.n.NezahalPrimalTide;
import com.github.laxika.magicalvibes.cards.p.PouncingShoreshark;
import com.github.laxika.magicalvibes.cards.p.PursuedWhale;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.cards.r.RuinCrab;
import com.github.laxika.magicalvibes.cards.s.SeaDasherOctopus;
import com.github.laxika.magicalvibes.cards.s.SigiledStarfish;
import com.github.laxika.magicalvibes.cards.s.SpinedMegalodon;
import com.github.laxika.magicalvibes.cards.s.StingingLionfish;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.v.VoraciousGreatshark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OyaminartokPolarWerebear.class, Archipelagore.class, JunkWinder.class,
        MoatPiranhas.class, MysticSkyfish.class, NadirKraken.class, NezahalPrimalTide.class,
        PouncingShoreshark.class, PursuedWhale.class, RiptideTurtle.class, RuinCrab.class,
        SeaDasherOctopus.class, SigiledStarfish.class, SpinedMegalodon.class,
        StingingLionfish.class, VoraciousGreatshark.class, Food.class})
class OyaminartokPolarWerebearTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof until it deals damage")
    void hasHexproofUntilDealingDamage() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isTrue();

        werebear.setAttacking(true);
        resolveCombat();

        assertThat(gqs.hasKeyword(gd, werebear, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Creates a Food token when it deals combat damage to a player")
    void createsFoodTokenOnCombatDamageToPlayer() {
        Permanent werebear = addCreatureReady(player1, new OyaminartokPolarWerebear());
        werebear.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Sacrifices a Food to draft and then adds restricted blue mana")
    void sacrificesFoodToDraftAndAddRestrictedMana() {
        harness.addToBattlefield(player1, new OyaminartokPolarWerebear());
        harness.addToBattlefield(player1, new Food());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(3);
    }
}
