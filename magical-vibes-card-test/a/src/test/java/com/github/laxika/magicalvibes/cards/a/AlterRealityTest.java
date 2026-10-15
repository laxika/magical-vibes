package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.m.MajorTeroh;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlterReality.class, PaladinEnVec.class, MajorTeroh.class, BaskingRootwalla.class, NantukoShade.class})
class AlterRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word on a target permanent indefinitely")
    void changesColorWordOnTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getTextReplacements()).containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Changes the color named by protection and lasts indefinitely")
    void changesColorWordInProtectionAbilityIndefinitely() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isFalse();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Carries a text change from a target spell onto the permanent it becomes")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new AlterReality(), new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 1);
        UUID paladinSpellId = gd.stack.getFirst().getCard().getId();
        harness.castAndResolveInstant(player1, 0, paladinSpellId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Paladin en-Vec").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Flashback changes a permanent's text and exiles Alter Reality")
    void flashbackChangesPermanentTextAndExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setGraveyard(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "WHITE");

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("black", "white"));
        harness.assertNotInGraveyard(player1, "Alter Reality");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Alter Reality"));
    }

    @Test
    @DisplayName("Changing black to green rewrites a permanent's activated exile ability")
    void changesColorWordInActivatedAbility() {
        Permanent teroh = harness.addToBattlefieldAndReturn(player1, new MajorTeroh());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new BaskingRootwalla());
        harness.setHand(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, teroh.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blackCreature).doesNotContain(greenCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(greenCreature.getCard());
        harness.assertInGraveyard(player1, "Major Teroh");
    }

    @Test
    @DisplayName("A spell with no color words can be targeted without changing its behavior")
    void targetsSpellWithoutColorWords() {
        harness.setHand(player1, List.of(new AlterReality(), new BaskingRootwalla()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 1);
        UUID creatureSpellId = gd.stack.getFirst().getCard().getId();
        harness.castAndResolveInstant(player1, 0, creatureSpellId);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "BLUE");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Basking Rootwalla");
        harness.assertInGraveyard(player1, "Alter Reality");
    }
}
