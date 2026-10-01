package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "66")
@CardRegistration(set = "WHO", collectorNumber = "671")
public class DeathInHeaven extends Card {

    public DeathInHeaven() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, millThenExileTargetPlayerGraveyard());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(anyPlayerTargetGroup()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, millThenExileTargetPlayerGraveyard());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II, List.of(anyPlayerTargetGroup()));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                ReturnAllCardsExiledWithSourceEffect.faceDownUnderControllerControl(
                        new CardTypePredicate(CardType.CREATURE), CardSubtype.CYBERMAN));
    }

    private CardEffect millThenExileTargetPlayerGraveyard() {
        return SequenceEffect.of(
                new MillEffect(2, MillRecipient.TARGET_PLAYER),
                new ExileGraveyardCardsEffect(
                        0, GraveyardExileScope.TARGET_PLAYER_ENTIRE,
                        null, null, false, true, false));
    }

    private SagaChapterTargetGroup anyPlayerTargetGroup() {
        return new SagaChapterTargetGroup(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                1, 1);
    }
}
