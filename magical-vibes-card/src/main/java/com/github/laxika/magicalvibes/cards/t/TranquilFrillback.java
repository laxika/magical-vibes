package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaUpToNTimesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "24")
public class TranquilFrillback extends Card {

    public TranquilFrillback() {
        DestroyTargetPermanentEffect destroy = new DestroyTargetPermanentEffect();
        ExileGraveyardCardsEffect exileGraveyard =
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE);

        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment"
        ), 0, 1);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"
        ), 0, 1);
        registerEffectTargetIndex(destroy, 0);
        registerEffectTargetIndex(exileGraveyard, 1);

        ChooseOneEffect modes = ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption("Destroy target artifact or enchantment", destroy),
                new ChooseOneEffect.ChooseOneOption("Exile target player's graveyard", exileGraveyard),
                new ChooseOneEffect.ChooseOneOption("You gain 4 life", new GainLifeEffect(4))
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayManaUpToNTimesEffect("{G}", 3, modes));
    }
}
