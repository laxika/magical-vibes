package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeigaTheTideStar.class, MossKami.class, RendSpirit.class, EiganjoCastle.class, ImprisonedInTheMoon.class})
class KeigaTheTideStarTest extends BaseCardTest {

    @Test
    @DisplayName("When Keiga dies, its controller gains control of target creature permanently")
    void diesGainsControlOfTargetCreature() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new MossKami());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        UUID keigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID mossKamiId = harness.getPermanentId(player2, "Moss Kami");

        harness.castInstant(player2, 0, keigaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, mossKamiId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moss Kami");
        harness.assertOnBattlefield(player1, "Moss Kami");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Moss Kami");
    }

    @Test
    @DisplayName("Death trigger targets creatures only, not lands")
    void deathTriggerOffersCreaturesOnly() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new MossKami());
        harness.addToBattlefield(player2, new EiganjoCastle());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        UUID keigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID mossKamiId = harness.getPermanentId(player2, "Moss Kami");
        UUID castleId = harness.getPermanentId(player2, "Eiganjo Castle");

        harness.castInstant(player2, 0, keigaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mossKamiId)
                .doesNotContain(castleId);
    }

    @Test
    @DisplayName("Death trigger cannot gain control of a target that stops being a creature before resolution")
    void targetMustStillBeCreatureAtResolution() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new MossKami());
        setupPlayer2Active();

        harness.setHand(player2, List.of(new RendSpirit(), new ImprisonedInTheMoon()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID keigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID mossKamiId = harness.getPermanentId(player2, "Moss Kami");

        harness.castInstant(player2, 0, keigaId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mossKamiId);

        harness.castEnchantment(player2, 0, mossKamiId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Moss Kami");
        harness.assertNotOnBattlefield(player1, "Moss Kami");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
