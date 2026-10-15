package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MonasterySwiftspear;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TLDR.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, SerraAngel.class, SoulWarden.class, MonasterySwiftspear.class})
class TLDRTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with a non-keyword ability")
    void exilesCreatureWithNonKeywordAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castTldr(target);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Does not exile a vanilla or keyword-only creature")
    void doesNotExileVanillaOrKeywordOnlyCreature() {
        Permanent vanilla = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent keywordOnly = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castTldr(vanilla);
        castTldr(keywordOnly);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not exile a creature with a triggered keyword ability")
    void doesNotExileCreatureWithTriggeredKeywordAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonasterySwiftspear());

        castTldr(target);

        harness.assertOnBattlefield(player2, "Monastery Swiftspear");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature with a non-keyword triggered ability")
    void exilesCreatureWithNonKeywordTriggeredAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SoulWarden());

        castTldr(target);

        harness.assertNotOnBattlefield(player2, "Soul Warden");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Soul Warden"));
    }

    @Test
    @DisplayName("Can exile its controller's creature")
    void exilesControllersCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        castTldr(target);

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsNonCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TLDR()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castTldr(Permanent target) {
        harness.setHand(player1, List.of(new TLDR()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
