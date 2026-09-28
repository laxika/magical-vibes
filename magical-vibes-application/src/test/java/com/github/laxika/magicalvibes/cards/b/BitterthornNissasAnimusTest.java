package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterthornNissasAnimus.class, GrizzlyBears.class, Forest.class})
class BitterthornNissasAnimusTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates and equips a Phyrexian Germ")
    void livingWeaponCreatesAndEquipsGerm() {
        harness.setHand(player1, List.of(new BitterthornNissasAnimus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Bitterthorn, Nissa's Animus");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(equipment.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with the equipped creature may search for a tapped basic land")
    void attackMaySearchForBasicLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addBitterthornReady(player1);
        equipment.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    private Permanent addBitterthornReady(Player player) {
        Permanent permanent = new Permanent(new BitterthornNissasAnimus());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
