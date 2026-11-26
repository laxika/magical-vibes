package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "18")
@CardRegistration(set = "PIP", collectorNumber = "367")
@CardRegistration(set = "PIP", collectorNumber = "546")
@CardRegistration(set = "PIP", collectorNumber = "895")
public class Overencumbered extends Card {

    public Overencumbered() {
        setEnchantPlayer(true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofClueToken(1)
                                .withInvestigate(false)
                                .withSimultaneousTokens(List.of(
                                        CreateTokenEffect.ofFoodToken(1),
                                        junkToken()))));
        // At the beginning of combat on enchanted opponent's turn, that player may pay {1} for each artifact they
        // control. If they don't, creatures can't attack this combat. (It is their turn: the active player's artifacts.)
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ENCHANTED_PLAYER), UnlessPaysEffect.unlessPays(
                Subjects.enchantedPlayer(),
                PayManaCost.withGenericIncrease("{0}",
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.ACTIVE_PLAYER)),
                new CantAttackThisTurnEffect(Subjects.allCreatures(), Duration.UNTIL_END_OF_COMBAT)));
    }

    private static CreateTokenEffect junkToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
