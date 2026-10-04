package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AmberPrison;
import com.github.laxika.magicalvibes.cards.h.HallOfGemstone;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.cards.s.SkyDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Illumination.class, AmberPrison.class, HallOfGemstone.class, MtendaLion.class, SkyDiamond.class, HangarbackWalker.class})
class IlluminationTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell and its controller gains life equal to its mana value")
    void countersArtifactSpell() {
        SkyDiamond skyDiamond = new SkyDiamond();
        harness.castFromHand(player1, skyDiamond, "{2}");
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, skyDiamond.getId());

        harness.assertInGraveyard(player1, "Sky Diamond");
        harness.assertNotOnBattlefield(player1, "Sky Diamond");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counters an enchantment spell and its controller gains life equal to its mana value")
    void countersEnchantmentSpell() {
        HallOfGemstone hallOfGemstone = new HallOfGemstone();
        harness.castFromHand(player1, hallOfGemstone, "{1}{G}{G}");
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hallOfGemstone.getId());

        harness.assertInGraveyard(player1, "Hall of Gemstone");
        harness.assertNotOnBattlefield(player1, "Hall of Gemstone");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        MtendaLion mtendaLion = new MtendaLion();
        harness.castFromHand(player1, mtendaLion, "{G}");

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mtendaLion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster of Illumination gains no life")
    void casterGainsNoLife() {
        SkyDiamond skyDiamond = new SkyDiamond();
        harness.castFromHand(player1, skyDiamond, "{2}");

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.setLife(player2, 20);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, skyDiamond.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts both X symbols in an artifact creature spell's mana value")
    @CardUsed({Illumination.class, HangarbackWalker.class})
    void gainsLifeForEveryXSymbol() {
        HangarbackWalker walker = new HangarbackWalker();
        harness.setHand(player1, List.of(walker));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, walker.getId());

        harness.assertInGraveyard(player1, "Hangarback Walker");
        harness.assertNotOnBattlefield(player1, "Hangarback Walker");
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can counter its caster's own artifact spell and give that player life")
    void countersOwnSpell() {
        SkyDiamond diamond = new SkyDiamond();
        harness.castFromHand(player1, diamond, "{2}");
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Illumination()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, diamond.getId());

        harness.assertInGraveyard(player1, "Sky Diamond");
        harness.assertNotOnBattlefield(player1, "Sky Diamond");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains no additional life when the target was already countered")
    void noLifeGainWhenTargetLeavesStack() {
        SkyDiamond diamond = new SkyDiamond();
        harness.castFromHand(player1, diamond, "{2}");
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Illumination(), new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, diamond.getId());
        harness.castAndResolveInstant(player2, 0, diamond.getId());
        harness.assertLife(player1, 22);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Sky Diamond");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an activated ability from an artifact source")
    void cannotTargetActivatedAbility() {
        AmberPrison amberPrison = new AmberPrison();
        harness.addToBattlefieldAndReturn(player1, amberPrison).setSummoningSick(false);
        SkyDiamond target = new SkyDiamond();
        harness.addToBattlefield(player2, target);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Sky Diamond"));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, amberPrison.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target a triggered ability from an enchantment source")
    void cannotTargetTriggeredAbility() {
        HallOfGemstone hallOfGemstone = new HallOfGemstone();
        harness.addToBattlefield(player1, hallOfGemstone);
        advanceToUpkeep(player1);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hallOfGemstone.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
    }
}
