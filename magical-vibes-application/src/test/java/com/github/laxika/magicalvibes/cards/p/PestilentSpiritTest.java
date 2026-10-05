package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.k.KasminasTransmutation;
import com.github.laxika.magicalvibes.cards.r.RubblebeltRecluse;
import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.cards.s.SkewerTheCritics;
import com.github.laxika.magicalvibes.cards.t.TitanicBrawl;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestilentSpirit.class, EarthElemental.class, Shock.class, Firebolt.class,
        RubblebeltRecluse.class, Scorchmark.class, SkewerTheCritics.class, KasminasTransmutation.class,
        SenateCourier.class, TitanicBrawl.class})
class PestilentSpiritTest extends BaseCardTest {

    @Test
    @CardUsed({PestilentSpirit.class, EarthElemental.class, Shock.class})
    @DisplayName("Your instant spell's damage destroys a creature through deathtouch")
    void instantSpellDamageHasDeathtouch() {
        addCreatureReady(player1, new PestilentSpirit());
        addCreatureReady(player2, new EarthElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Earth Elemental");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @CardUsed({PestilentSpirit.class, EarthElemental.class, Firebolt.class})
    @DisplayName("Your sorcery spell's damage destroys a creature through deathtouch")
    void sorcerySpellDamageHasDeathtouch() {
        addCreatureReady(player1, new PestilentSpirit());
        addCreatureReady(player2, new EarthElemental());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Earth Elemental");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @CardUsed({PestilentSpirit.class, RubblebeltRecluse.class, SkewerTheCritics.class})
    void opponentsSpellsDoNotGainDeathtouch() {
        addCreatureReady(player2, new PestilentSpirit());
        var target = addCreatureReady(player2, new RubblebeltRecluse());
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Rubblebelt Recluse");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({PestilentSpirit.class, RubblebeltRecluse.class, SkewerTheCritics.class, Scorchmark.class})
    void removingSpiritBeforeResolutionRemovesSpellDeathtouch() {
        var spirit = addCreatureReady(player1, new PestilentSpirit());
        var target = addCreatureReady(player2, new RubblebeltRecluse());
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pestilent Spirit");
        harness.assertOnBattlefield(player2, "Rubblebelt Recluse");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({PestilentSpirit.class, SkewerTheCritics.class})
    void deathtouchDoesNotChangeDamageToPlayers() {
        addCreatureReady(player1, new PestilentSpirit());
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @CardUsed({PestilentSpirit.class, RubblebeltRecluse.class, SkewerTheCritics.class, KasminasTransmutation.class})
    void spiritThatLostAllAbilitiesDoesNotGrantSpellDeathtouch() {
        var spirit = addCreatureReady(player1, new PestilentSpirit());
        var target = addCreatureReady(player2, new RubblebeltRecluse());
        harness.setHand(player1, List.of(new KasminasTransmutation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, spirit.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SkewerTheCritics()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Rubblebelt Recluse");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({PestilentSpirit.class, SenateCourier.class, RubblebeltRecluse.class, TitanicBrawl.class})
    void fightDamageDoesNotInheritTheSpellsDeathtouch() {
        addCreatureReady(player1, new PestilentSpirit());
        var courier = addCreatureReady(player1, new SenateCourier());
        var target = addCreatureReady(player2, new RubblebeltRecluse());
        harness.setHand(player1, List.of(new TitanicBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(courier.getId(), target.getId()));

        harness.assertInGraveyard(player1, "Senate Courier");
        harness.assertOnBattlefield(player2, "Rubblebelt Recluse");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
