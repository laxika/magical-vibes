package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "126")
public class DownDirty extends Card {

    public DownDirty() {
        TargetFilter player = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        TargetFilter graveyardCard = new GraveyardCardPredicateTargetFilter(
                null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        CardEffect down = new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER);
        CardEffect dirty = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Down — Target player discards two cards", down, player
                ).withManaCost("{3}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Dirty — Return target card from your graveyard to your hand", dirty, graveyardCard
                ).withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Down and then Dirty",
                        List.of(down, dirty),
                        List.of(player, graveyardCard)
                ).withManaCost("{5}{B}{G}")
        )));
    }
}
