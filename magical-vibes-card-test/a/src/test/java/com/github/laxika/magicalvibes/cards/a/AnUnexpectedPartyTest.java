package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StaunchShieldmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnUnexpectedParty.class, AtTheDoor.class, StaunchShieldmate.class, GrizzlyBears.class})
class AnUnexpectedPartyTest extends BaseCardTest {

    @Test
    void choosesCreatureTypeAndBoostsMatchingCreatures() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new StaunchShieldmate());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingDwarf = harness.addToBattlefieldAndReturn(player2, new StaunchShieldmate());

        harness.castFromHand(player1, new AnUnexpectedParty(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.DWARF.name());

        Permanent party = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AnUnexpectedParty)
                .findFirst()
                .orElseThrow();
        assertThat(party.getChosenSubtype()).isEqualTo(CardSubtype.DWARF);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingDwarf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingDwarf)).isEqualTo(3);
    }

    @Test
    void adventureCreatesXRedDwarfTokensAndExilesTheCard() {
        AnUnexpectedParty card = new AnUnexpectedParty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAdventure(player1, 0, 2, Map.of());
        harness.passBothPriorities();

        List<Permanent> dwarves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(dwarves).hasSize(2);
        assertThat(dwarves).allSatisfy(dwarf -> {
            assertThat(dwarf.getCard().getName()).isEqualTo("Dwarf");
            assertThat(dwarf.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(dwarf.getCard().getSubtypes()).contains(CardSubtype.DWARF);
            assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(2);
        });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void enchantmentFaceCanBeCastFromExileAfterAdventure() {
        AnUnexpectedParty card = new AnUnexpectedParty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.DWARF.name());

        harness.assertOnBattlefield(player1, "An Unexpected Party");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void boostsAdventureTokensAlreadyPresentAndCreatedLater() {
        AnUnexpectedParty card = new AnUnexpectedParty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, 1, Map.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.DWARF.name());

        List<Permanent> existingTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(existingTokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, existingTokens.getFirst())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existingTokens.getFirst())).isEqualTo(4);

        harness.setHand(player1, List.of(new AnUnexpectedParty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, 1, Map.of());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        });
    }
}
