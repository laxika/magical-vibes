package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.w.WirewoodPride;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtificialEvolution.class, AvenBrigadier.class, ElvishWarrior.class})
class ArtificialEvolutionTest extends BaseCardTest {

    @Test
    void changesCreatureTypeTextOnPermanent() {
        harness.addToBattlefield(player1, new AvenBrigadier());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        UUID targetId = harness.getPermanentId(player1, "Aven Brigadier");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BIRD");
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }

    @Test
    void changesCreatureTypeOnPermanentTypeLine() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, warrior.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        harness.handleListChoice(player1, "BIRD");

        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.BIRD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.ELF)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.WARRIOR)).isTrue();
    }

    @Test
    void changedSpellTypeLineCarriesToPermanent() {
        harness.setHand(player1, List.of(new ElvishWarrior(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        harness.handleListChoice(player1, "BIRD");
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.BIRD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.ELF)).isFalse();
    }

    @Test
    @CardUsed(WirewoodPride.class)
    void changesCreatureTypeInSpellCountingEffect() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new WirewoodPride(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, warrior.getId());
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        harness.handleListChoice(player1, "BIRD");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
    }

    @Test
    void canReplaceWallRestrictionOnAnotherArtificialEvolution() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ArtificialEvolution(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, warrior.getId());
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WALL");
        harness.handleListChoice(player1, "BIRD");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        harness.handleListChoice(player1, "WALL");

        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.WALL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warrior, CardSubtype.ELF)).isFalse();
    }

    @Test
    void cannotChooseWallAsReplacementCreatureType() {
        harness.addToBattlefield(player1, new AvenBrigadier());
        UUID targetId = harness.getPermanentId(player1, "Aven Brigadier");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BIRD");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "WALL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyCreatureTypesCanBeChosen() {
        harness.addToBattlefield(player1, new AvenBrigadier());
        UUID targetId = harness.getPermanentId(player1, "Aven Brigadier");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLUE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creatureTypeChangeOnSpellCarriesToPermanent() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new AvenBrigadier()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BIRD");
        harness.handleListChoice(player1, "ELF");
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }

    @Test
    void creatureTypeChangeLastsBeyondEndOfTurn() {
        harness.addToBattlefield(player1, new AvenBrigadier());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        UUID targetId = harness.getPermanentId(player1, "Aven Brigadier");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BIRD");
        harness.handleListChoice(player1, "ELF");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }
}
