package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.VisceraSeer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlassCastHeart.class, VisceraSeer.class})
class GlassCastHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Blood token when a Vampire attacks")
    void createsBloodTokenWhenVampireAttacks() {
        addHeart();
        addVampire(player1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void multipleAttackingVampiresCreateOnlyOneBloodToken() {
        addHeart();
        addVampire(player1);
        addVampire(player1);

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void opponentsAttackingVampireDoesNotCreateBlood() {
        addHeart();
        addVampire(player2);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void attackTriggerResolvesAfterVampireLeavesBattlefield() {
        addHeart();
        addVampire(player1);
        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void cannotActivateDrainWithOnlyTwelveBloodTokens() {
        Permanent heart = addHeart();
        for (int i = 0; i < 12; i++) {
            addBloodToken(player1);
        }
        addBloodToken(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heart);
        assertThat(heart.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Blood")).hasSize(12);
    }

    @Test
    void vampireCreationPaysLifeAndTapsBeforeResolution() {
        Permanent heart = addHeart();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(heart.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Vampire")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    @DisplayName("Pays life and creates a lifelink Vampire token")
    void createsLifelinkVampireToken() {
        addHeart();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Vampire")
                        && permanent.getCard().getColors().size() == 2
                        && permanent.getCard().getColors().containsAll(List.of(CardColor.WHITE, CardColor.BLACK))
                        && permanent.getCard().getSubtypes().contains(CardSubtype.VAMPIRE)
                        && permanent.getCard().getKeywords().contains(Keyword.LIFELINK)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Sacrifices the Heart and thirteen Blood tokens to drain each opponent")
    void sacrificesHeartAndThirteenBloodTokens() {
        addHeart();
        for (int i = 0; i < 13; i++) {
            addBloodToken(player1);
        }
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(33);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
        assertThat(findPermanents(player1, "Glass-Cast Heart")).isEmpty();
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    private Permanent addHeart() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GlassCastHeart());
        heart.setSummoningSick(false);
        return heart;
    }

    private void addVampire(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new VisceraSeer());
        permanent.setSummoningSick(false);
    }

    private void addBloodToken(Player player) {
        Card blood = new Card();
        blood.setName("Blood");
        blood.setType(CardType.ARTIFACT);
        blood.setSubtypes(List.of(CardSubtype.BLOOD));
        blood.setToken(true);
        harness.addToBattlefield(player, blood);
    }
}
