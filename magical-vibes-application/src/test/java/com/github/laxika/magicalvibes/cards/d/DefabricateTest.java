package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.s.ScrapworkMutt;
import com.github.laxika.magicalvibes.cards.s.StaticNet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Defabricate.class, FumeSpitter.class, GrizzlyBears.class, JayemdaeTome.class,
        ScrapworkMutt.class, StaticNet.class})
class DefabricateTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an enchantment spell and exiles it before it enters")
    void countersEnchantmentSpellAndExilesIt() {
        StaticNet net = new StaticNet();
        harness.setHand(player1, List.of(net));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, net.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(net);
        harness.assertNotInGraveyard(player1, "Static Net");
        harness.assertNotOnBattlefield(player1, "Static Net");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifact creatures are legal for the spell mode but not the ability mode")
    void countersArtifactCreatureSpell() {
        ScrapworkMutt mutt = new ScrapworkMutt();
        harness.setHand(player1, List.of(mutt));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, mutt.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castInstant(player2, 0, 0, mutt.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(mutt);
        harness.assertNotOnBattlefield(player1, "Scrapwork Mutt");
        harness.assertNotInGraveyard(player1, "Scrapwork Mutt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a triggered ability without removing its artifact source")
    void countersTriggeredAbilityButNotItsSource() {
        harness.setHand(player1, List.of(new ScrapworkMutt(), new StaticNet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var abilityId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
        harness.castInstant(player2, 0, 1, abilityId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scrapwork Mutt");
        harness.assertInHand(player1, "Static Net");
        harness.assertNotInGraveyard(player1, "Scrapwork Mutt");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an artifact spell and exiles it")
    void countersArtifactSpellAndExilesIt() {
        JayemdaeTome tome = new JayemdaeTome();
        harness.setHand(player1, List.of(tome));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, tome.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(tome.getId()));
        harness.assertNotInGraveyard(player1, "Jayemdae Tome");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot use the artifact or enchantment mode on a creature spell")
    void cannotTargetCreatureSpellWithArtifactOrEnchantmentMode() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an activated ability")
    void countersActivatedAbility() {
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player1, fumeSpitter);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, fumeSpitter.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fume Spitter");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
