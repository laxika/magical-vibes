package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychoticFury.class, AssaultZeppelid.class, MistralCharger.class, SealOfDoom.class})
class PsychoticFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Grants double strike to a multicolored creature and draws a card")
    void grantsDoubleStrikeAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInHand(player1, "Mistral Charger");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a multicolored creature")
    void rejectsMonocoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored creature");
    }

    @Test
    @DisplayName("Can target a multicolored creature controlled by an opponent")
    void canTargetOpponentsMulticoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not draw when the only target is destroyed before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        harness.addToBattlefield(player2, new SealOfDoom());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Assault Zeppelid");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Psychotic Fury");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
