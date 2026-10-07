package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Transcantation.class, Unsummon.class, GrizzlyBears.class, Divination.class, Geistflame.class})
class TranscantationTest extends BaseCardTest {

    @Test
    void decliningNewTargetMakesTransformedSpellFailToResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();

        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unsummon.getId());

        GameData gameData = harness.getGameData();
        StackEntry transformed = gameData.stack.getFirst();
        assertThat(transformed.getCard().getName()).isEqualTo("Lightning Bolt");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Unsummon");
        harness.assertNotInHand(player1, "Unsummon");
    }

    @Test
    void canRetargetTheTransformedSpell() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();

        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unsummon.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(originalTarget)
                .doesNotContain(newTarget);
    }

    @Test
    void cannotTargetCreatureSpell() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getGameData().stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseTheOriginalCreatureAsTheNewTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();
        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, unsummon.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unsummon");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void untargetedSorceryCanGainATargetAndDealsDamageInsteadOfDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Divination divination = new Divination();
        harness.castFromHand(player1, divination, "{2}{U}");
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.assertInGraveyard(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void transformedFlashbackSpellGoesToGraveyardInsteadOfExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Geistflame geistflame = new Geistflame();
        harness.setGraveyard(player1, List.of(geistflame));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, geistflame.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Geistflame");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
