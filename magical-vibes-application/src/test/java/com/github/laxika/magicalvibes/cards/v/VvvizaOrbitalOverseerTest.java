package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VvvizaOrbitalOverseer.class, GrizzlyBears.class,
        AdagiaWindsweptBastion.class, EvendoWakingHaven.class, KavaronMemorialWorld.class,
        SusurSecundiVoidAltar.class, UthrosTitanicGodcore.class})
class VvvizaOrbitalOverseerTest extends BaseCardTest {

    @Test
    void enteringOffersEveryPlanetFromTheSpellbook() {
        harness.enterBattlefieldAndReturn(player1, new VvvizaOrbitalOverseer());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Adagia, Windswept Bastion");

        assertThat(findPermanent(player1, "Adagia, Windswept Bastion")).isNotNull();
    }

    @Test
    void attackingCreatesAFlyingLanderCreature() {
        addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        assertThat(lander.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(lander.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(lander.getCard().getPower()).isEqualTo(2);
        assertThat(lander.getCard().getToughness()).isEqualTo(1);
        assertThat(lander.getCard().getKeywords()).contains(Keyword.FLYING);
    }
}
