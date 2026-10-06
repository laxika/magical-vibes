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

        harness.castAndResolveInstant(player2, 0, keigaId);

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

        harness.castAndResolveInstant(player2, 0, keigaId);

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

        harness.castAndResolveInstant(player2, 0, keigaId);
        harness.handlePermanentChosen(player1, mossKamiId);

        var aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(mossKamiId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Moss Kami");
        harness.assertNotOnBattlefield(player1, "Moss Kami");
    }

    @Test
    @DisplayName("Death trigger can target a creature its controller already controls")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        var mossKami = harness.addToBattlefieldAndReturn(player1, new MossKami());
        mossKami.tap();
        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Keiga, the Tide Star"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mossKami.getId());
        harness.handlePermanentChosen(player1, mossKami.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moss Kami");
        assertThat(mossKami.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Keiga, the Tide Star");
    }

    @Test
    @DisplayName("Death trigger does not wait for a target when no creatures remain")
    void deathWithNoLegalTargets() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new EiganjoCastle());
        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Keiga, the Tide Star"));

        harness.assertInGraveyard(player1, "Keiga, the Tide Star");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Eiganjo Castle");
    }

    @Test
    @DisplayName("Death trigger has no effect if its target dies in response")
    void targetDiesBeforeResolution() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new MossKami());
        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit(), new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        UUID mossKamiId = harness.getPermanentId(player2, "Moss Kami");

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Keiga, the Tide Star"));
        harness.handlePermanentChosen(player1, mossKamiId);
        harness.castAndResolveInstant(player2, 0, mossKamiId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moss Kami");
        harness.assertNotOnBattlefield(player1, "Moss Kami");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen Keiga's death trigger belongs to its controller, not its owner")
    void stolenKeigaTriggersForItsController() {
        harness.addToBattlefield(player1, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new KeigaTheTideStar());
        harness.addToBattlefield(player2, new MossKami());
        UUID firstKeigaId = harness.getPermanentId(player1, "Keiga, the Tide Star");
        UUID secondKeigaId = harness.getPermanentId(player2, "Keiga, the Tide Star");
        UUID mossKamiId = harness.getPermanentId(player2, "Moss Kami");
        setupPlayer2Active();
        harness.setHand(player2, List.of(new RendSpirit(), new RendSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player2, 0, firstKeigaId);
        harness.handlePermanentChosen(player1, secondKeigaId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Keiga, the Tide Star");
        harness.assertNotOnBattlefield(player2, "Keiga, the Tide Star");

        harness.castAndResolveInstant(player2, 0, secondKeigaId);
        harness.handlePermanentChosen(player1, mossKamiId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moss Kami");
        harness.assertNotOnBattlefield(player2, "Moss Kami");
        harness.assertInGraveyard(player2, "Keiga, the Tide Star");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
