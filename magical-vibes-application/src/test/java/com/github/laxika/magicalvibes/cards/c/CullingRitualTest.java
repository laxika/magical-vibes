package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PestSummoning;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.SpellSatchel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CullingRitual.class, GrizzlyBears.class, Mountain.class, Ornithopter.class,
        RuleOfLaw.class, DarksteelRelic.class, PestSummoning.class, SpellSatchel.class})
class CullingRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonland permanents with mana value 2 or less and adds mana for each")
    void destroysMatchingPermanentsAndAddsMana() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rule of Law");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adds no mana and asks no color choice when nothing is destroyed")
    void addsNoManaWhenNothingMatches() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rule of Law");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Indestructible permanents survive and do not contribute mana")
    void indestructiblePermanentsDoNotCount() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player2, new Ornithopter());

        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Regeneration saves a matching creature without contributing mana")
    void regeneratedPermanentDoesNotCount() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getRegenerationShield()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Destroyed tokens each contribute mana and all mana may be the same color")
    void destroyedTokensCountAndAllowSameColor() {
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Pest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Destroys an eligible noncreature artifact and gives mana to the spell controller")
    void destroysNoncreatureArtifact() {
        harness.addToBattlefield(player2, new SpellSatchel());

        harness.castFromHand(player1, new CullingRitual(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Spell Satchel");
        harness.assertNotOnBattlefield(player2, "Spell Satchel");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
