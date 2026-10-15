package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GolgariKeyrune;
import com.github.laxika.magicalvibes.cards.l.LotlethTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupremeVerdict.class, Cancel.class, GloriousAnthem.class, GrizzlyBears.class,
        GolgariKeyrune.class, LotlethTroll.class})
class SupremeVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures but not other permanents")
    void destroysAllCreaturesButNotOtherPermanents() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new SupremeVerdict()));
        addVerdictMana(player1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SupremeVerdict verdict = new SupremeVerdict();

        harness.setHand(player1, List.of(verdict));
        addVerdictMana(player1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, verdict.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Supreme Verdict");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows a creature to regenerate from the destruction")
    void allowsRegeneration() {
        var troll = harness.addToBattlefieldAndReturn(player2, new LotlethTroll());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SupremeVerdict()));
        addVerdictMana(player1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lotleth Troll");
        harness.assertNotInGraveyard(player2, "Lotleth Troll");
        assertThat(troll.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    @Test
    @DisplayName("Destroys animated artifacts while sparing noncreature artifacts")
    void destroysAnimatedArtifactsOnly() {
        harness.addToBattlefieldAndReturn(player1, new GolgariKeyrune());
        harness.addToBattlefieldAndReturn(player2, new GolgariKeyrune());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SupremeVerdict()));
        addVerdictMana(player1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Golgari Keyrune");
        harness.assertInGraveyard(player1, "Golgari Keyrune");
        harness.assertOnBattlefield(player2, "Golgari Keyrune");
        harness.assertNotInGraveyard(player2, "Golgari Keyrune");
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    @Test
    @DisplayName("Resolves without any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new SupremeVerdict()));
        addVerdictMana(player1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    private void addVerdictMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
