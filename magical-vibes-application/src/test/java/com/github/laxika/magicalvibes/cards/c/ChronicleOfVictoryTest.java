package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GutsplitterGang;
import com.github.laxika.magicalvibes.cards.k.KarnSilverGolem;
import com.github.laxika.magicalvibes.cards.l.LysAlanaInformant;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChronicleOfVictory.class, LysAlanaInformant.class, GutsplitterGang.class,
        Tarfire.class, KarnSilverGolem.class, AmoeboidChangeling.class})
class ChronicleOfVictoryTest extends BaseCardTest {

    private Permanent addChronicle(CardSubtype chosenSubtype) {
        Permanent chronicle = harness.addToBattlefieldAndReturn(player1, new ChronicleOfVictory());
        chronicle.setChosenSubtype(chosenSubtype);
        return chronicle;
    }

    @Test
    void resolvingChroniclePromptsForCreatureType() {
        harness.setHand(player1, List.of(new ChronicleOfVictory()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
        assertThat(findPermanent(player1, "Chronicle of Victory").getChosenSubtype()).isEqualTo(CardSubtype.ELF);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chosenTypeCreaturesGetBoostAndKeywords() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LysAlanaInformant());
        addChronicle(CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void differentTypeCreaturesDoNotGetBoostOrKeywords() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GutsplitterGang());
        addChronicle(CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castingChosenTypeNoncreatureSpellDrawsACard() {
        addChronicle(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.setLibrary(player1, List.of(new ChronicleOfVictory()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chronicle of Victory");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void castingDifferentTypeSpellDoesNotDraw() {
        addChronicle(CardSubtype.ELF);
        harness.setHand(player1, List.of(new GutsplitterGang()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void opponentsChosenTypeCreaturesDoNotGetBoostOrKeywords() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new LysAlanaInformant());
        addChronicle(CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, elf).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isZero();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castingChosenTypeCreatureDrawsBeforeCreatureResolves() {
        addChronicle(CardSubtype.ELF);
        harness.setHand(player1, List.of(new LysAlanaInformant()));
        harness.setLibrary(player1, List.of(new ChronicleOfVictory()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chronicle of Victory");
        harness.assertNotOnBattlefield(player1, "Lys Alana Informant");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentCastingChosenTypeSpellDoesNotDraw() {
        addChronicle(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void changelingSpellMatchesTheChosenType() {
        addChronicle(CardSubtype.ELF);
        harness.setHand(player1, List.of(new AmoeboidChangeling()));
        harness.setLibrary(player1, List.of(new ChronicleOfVictory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chronicle of Victory");
    }

    @Test
    void animatedChronicleOfChosenTypeGetsItsOwnKeywords() {
        Permanent chronicle = addChronicle(CardSubtype.ELF);
        harness.addToBattlefield(player1, new KarnSilverGolem());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, chronicle.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, 0, null, chronicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, chronicle)).contains(CardSubtype.ELF);
        assertThat(gqs.hasKeyword(gd, chronicle, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, chronicle, Keyword.TRAMPLE)).isTrue();
    }
}
