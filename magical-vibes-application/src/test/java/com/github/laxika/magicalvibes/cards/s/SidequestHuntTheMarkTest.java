package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.y.YiazmatUltimateMark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidequestHuntTheMark.class, YiazmatUltimateMark.class, GrizzlyBears.class,
        LeoninScimitar.class, LightningBolt.class})
class SidequestHuntTheMarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying up to one target creature")
    void entersByDestroyingTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SidequestHuntTheMark()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can enter without choosing a creature for its optional ETB")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new SidequestHuntTheMark()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creates a Treasure when an opponent's creature died this turn")
    void createsTreasureForOpponentCreatureDeath() {
        Permanent source = addSidequest(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithLightningBolt(player1, target);
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not create a Treasure when your creature died")
    void doesNotCreateTreasureForOwnCreatureDeath() {
        addSidequest(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        destroyWithLightningBolt(player1, target);
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Transforms after the created Treasure brings the total to three")
    void transformsAtThreeTreasures() {
        Permanent source = addSidequest(player1);
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithLightningBolt(player1, target);
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(source.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Yiazmat sacrifices another creature to gain indestructible and tap")
    void yiazmatSacrificesCreature() {
        Permanent yiazmat = addTransformedYiazmat(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, yiazmat), null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(yiazmat.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, yiazmat, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Yiazmat sacrifices another artifact to pay its ability")
    void yiazmatSacrificesArtifact() {
        Permanent yiazmat = addTransformedYiazmat(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, yiazmat), null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(yiazmat.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, yiazmat, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Yiazmat cannot sacrifice itself")
    void yiazmatRequiresAnotherPermanent() {
        Permanent yiazmat = addTransformedYiazmat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, yiazmat), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSidequest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SidequestHuntTheMark());
    }

    private Permanent addTransformedYiazmat(Player player) {
        SidequestHuntTheMark front = new SidequestHuntTheMark();
        Permanent yiazmat = new Permanent(front);
        yiazmat.setSummoningSick(false);
        yiazmat.setCard(front.getBackFaceCard());
        yiazmat.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(yiazmat);
        return yiazmat;
    }

    private void destroyWithLightningBolt(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new LightningBolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        return card;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
