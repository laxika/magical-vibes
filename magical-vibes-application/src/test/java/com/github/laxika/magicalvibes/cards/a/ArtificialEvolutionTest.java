package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
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
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent warrior = findPermanent(player1, "Elvish Warrior");
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }
}
