package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class TibaltCosmicImpostor extends Card {

    public TibaltCosmicImpostor() {
        AllowCastFromCardsExiledWithSourceEffect permission =
                new AllowCastFromCardsExiledWithSourceEffect(
                        true, null, false, false, 0, null,
                        false, false, false, true);
        addEffect(EffectSlot.STATIC, permission);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateEmblemEffect(
                List.of(permission),
                "You may play cards exiled with Tibalt, Cosmic Impostor, and you may spend mana as though it were mana of any color to cast those spells."
        ));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER)),
                "+2: Exile the top card of each player's library."
        ));
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentEffect()),
                "−3: Exile target artifact or creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature"
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS),
                        new AwardManaEffect(ManaColor.RED, 3)
                ),
                "−8: Exile all graveyards. Add {R}{R}{R}."
        ));
    }
}
