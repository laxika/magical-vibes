package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "13")
public class Farewell extends Card {

    public Farewell() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all artifacts",
                        new ExileAllPermanentsEffect(new PermanentIsArtifactPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all creatures",
                        new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all enchantments",
                        new ExileAllPermanentsEffect(new PermanentIsEnchantmentPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all graveyards",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS))
        )));
    }
}
