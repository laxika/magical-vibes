package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThryxTheSuddenStorm.class, ColossalDreadmaw.class, Fireball.class, GrizzlyBears.class,
        Cancel.class, HangarbackWalker.class})
class ThryxTheSuddenStormTest extends BaseCardTest {

    @Test
    @DisplayName("Spells with mana value 5 or greater cost {1} less to cast")
    void reducesHighManaValueSpellCost() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setHand(player1, List.of(dreadmaw));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(dreadmaw.getId()));
    }

    @Test
    @DisplayName("Spells with mana value less than 5 are not reduced")
    void doesNotReduceLowManaValueSpellCost() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An announced X value contributes to the mana-value threshold")
    void includesAnnouncedXValueInManaValue() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 4, List.of(player2.getId()));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A high-mana-value spell you cast cannot be countered")
    void protectsHighManaValueSpellsYouCast() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setHand(player1, List.of(dreadmaw));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dreadmaw.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(dreadmaw.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(cancel.getId()));
    }

    @Test
    @DisplayName("A low-mana-value spell you cast can still be countered")
    void doesNotProtectLowManaValueSpells() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Thryx does not protect an opponent's high-mana-value spell")
    void doesNotProtectOpponentsSpells() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setHand(player2, List.of(dreadmaw));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, dreadmaw.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(dreadmaw.getId()));
    }

    @Test
    @DisplayName("Each X symbol contributes to counter protection")
    void protectsSpellWithTwoXSymbols() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        HangarbackWalker walker = new HangarbackWalker();
        harness.setHand(player1, List.of(walker));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, walker.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(walker.getId()));
        harness.assertNotInGraveyard(player1, "Hangarback Walker");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hangarback Walker");
    }

    @Test
    @DisplayName("Thryx entering in response protects a spell already on the stack")
    void enteringInResponseProtectsExistingSpell() {
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setHand(player1, List.of(dreadmaw, new ThryxTheSuddenStorm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dreadmaw.getId());
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thryx, the Sudden Storm");
        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(dreadmaw.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Thryx does not protect itself while it is a spell")
    void doesNotProtectItselfOnStack() {
        ThryxTheSuddenStorm thryx = new ThryxTheSuddenStorm();
        harness.setHand(player1, List.of(thryx));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, thryx.getId());

        harness.assertInGraveyard(player1, "Thryx, the Sudden Storm");
        harness.assertNotOnBattlefield(player1, "Thryx, the Sudden Storm");
    }

    @Test
    @DisplayName("Thryx does not discount an opponent's spell")
    void doesNotReduceOpponentsSpellCost() {
        harness.addToBattlefield(player1, new ThryxTheSuddenStorm());
        harness.setHand(player2, List.of(new ColossalDreadmaw()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
