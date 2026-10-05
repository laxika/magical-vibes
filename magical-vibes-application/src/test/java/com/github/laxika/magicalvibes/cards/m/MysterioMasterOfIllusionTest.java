package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DocOckSinisterScientist;
import com.github.laxika.magicalvibes.cards.l.LizardConnorssCurse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysterioMasterOfIllusion.class, DocOckSinisterScientist.class, LizardConnorssCurse.class})
class MysterioMasterOfIllusionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Illusion Villain for each nontoken Villain you control")
    void createsTokensForNontokenVillainsYouControl() {
        harness.addToBattlefield(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player2, new DocOckSinisterScientist());
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
        assertThat(findPermanents(player2, "Illusion Villain")).isEmpty();
    }

    @Test
    @DisplayName("Exiles the tokens it created when it leaves the battlefield")
    void exilesCreatedTokensWhenItLeaves() {
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mysterio = findPermanent(player1, "Mysterio, Master of Illusion");

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain");
        assertThat(tokens).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mysterio));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }

    @Test
    @DisplayName("A created token leaving does not affect Mysterio")
    void createdTokenLeavingDoesNotAffectMysterio() {
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Illusion Villain").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mysterio, Master of Illusion")).hasSize(1);
    }

    @Test
    @DisplayName("Leaving before token creation does not trigger token cleanup")
    void leavingBeforeTokenCreationDoesNotTriggerCleanup() {
        harness.addToBattlefield(player1, new DocOckSinisterScientist());
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();
        Permanent mysterio = findPermanent(player1, "Mysterio, Master of Illusion");
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mysterio));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Illusion Villain")).hasSize(1);
    }

    @Test
    @DisplayName("Counts nontoken Villains when the entry ability resolves")
    void countsVillainsAtResolution() {
        Permanent docOck = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, docOck));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).hasSize(1);
    }

    @Test
    @DisplayName("The delayed cleanup still exiles tokens after Mysterio loses all abilities")
    void cleanupSurvivesAbilityLoss() {
        harness.castFromHand(player1, new MysterioMasterOfIllusion(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent mysterio = findPermanent(player1, "Mysterio, Master of Illusion");
        assertThat(findPermanents(player1, "Illusion Villain")).hasSize(1);

        harness.setHand(player1, List.of(new LizardConnorssCurse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of(mysterio.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, mysterio)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mysterio));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }
}
